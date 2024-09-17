package ir.sss.parser;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class ScenarioRuleInit extends ScenarioRule {

    public ScenarioRuleInit(ScenarioRuleBuilder scenarioRuleBuilder) {
        super(scenarioRuleBuilder);
    }

    @Override
    public String getRule() {
        String templateString =
                """
                        rule "Attack {{scenario_id}} - Level {{attack_level}} - ID {{attack_rid}}"
                        when
                            $r: RequestEvent(\
                        {{#hasSrcIP}}sourceIp == {{event_src_ip}}, {{/hasSrcIP}}\
                        {{#hasSrcPort}}sourcePort == {{event_src_port}}, {{/hasSrcPort}}\
                        {{#hasDstIP}}destinationIp == {{event_dst_ip}}, {{/hasDstIP}}\
                        {{#hasDstPort}}destinationPort == {{event_dst_port}}, {{/hasDstPort}}\
                        pluginId == {{event_plugin_id}}, pluginSid == {{event_plugin_sid}})
                            not (Attack(sid == {{scenario_id}}, rid == {{attack_rid}}, events contains $r))
                            $clock: PseudoClockScheduler(currentTime < $r.getTimestamp().getTime() + 100000)
                            // eval(($clock.getCurrentTime() - $r.getTimestamp().getTime()) < 100000)
                        then
                            Scenario ns = new Scenario("{{scenario_id}}", "{{scenario_name}}");
                            {{#hasHostsPrep}}{{{hosts_preparation}}}{{/hasHostsPrep}}
                            Attack bfa = new Attack({{scenario_id}}, {{attack_rid}}, ns{{#hasHostsPrep}}, attack_src, attack_dst{{/hasHostsPrep}});
                            ArrayList<Event> eventsList = new ArrayList<>();
                            eventsList.add($r);
                            bfa.setEvents(eventsList);
                            ns.addAttack(bfa);
                            insert(bfa);
                            insert(ns);
                        end""";

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(templateString), "ruleTemplate");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("attack_level", getCurrentRuleParams().level);
        templateData.put("attack_rid", getCurrentRuleParams().rid);
        templateData.put("event_plugin_id", getCurrentRuleParams().pluginId);
        templateData.put("event_plugin_sid", getCurrentRuleParams().pluginSid);
        templateData.put("scenario_name", getScenarioParams().name);
        templateData.put("scenario_id", getScenarioParams().id);

        // Conditional logic for parameters
        String srcIP = getCurrentRuleParams().from;
        String srcPort = getCurrentRuleParams().portFrom;
        String dstIP = getCurrentRuleParams().to;
        String dstPort = getCurrentRuleParams().portTo;

        // Flags for conditional inclusion
        templateData.put("hasSrcIP", !srcIP.equals("ANY"));
        templateData.put("hasSrcPort", !srcPort.equals("ANY"));
        templateData.put("hasDstIP", !dstIP.equals("ANY"));
        templateData.put("hasDstPort", !dstPort.equals("ANY"));

        StringBuilder prepareAttackSource = new StringBuilder();
        StringBuilder prepareAttackDestination = new StringBuilder();
        StringBuilder prepareAttackHosts = new StringBuilder();

        if (srcIP.equals("ANY")) {
            prepareAttackSource.append("String attackSourceIp = $r.getSource().getIp();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(srcIP);
            Matcher ipMatcher = ipPattern.matcher(srcIP);

            if (referenceMatcher.find()) {
                templateData.put("event_src_ip", "WRONG INPUT; CANNOT REFER TO ANYTHING HERE!");
            } else if (ipMatcher.find()) {
                templateData.put("event_src_ip", srcIP);
                prepareAttackSource.append("String attackSourceIp = \"").append(srcIP).append("\";\n\t");
            } else {
                templateData.put("event_src_ip", "WRONG INPUT");
            }
        }

        if (srcPort.equals("ANY")) {
            prepareAttackSource.append("String attackSourcePort = $r.getSource().getPort();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(srcPort);
            Matcher portMatcher = portPattern.matcher(srcPort);

            if (referenceMatcher.find()) {
                templateData.put("event_src_port", "WRONG INPUT; CANNOT REFER TO ANYTHING HERE!");
            } else if (portMatcher.find()) {
                templateData.put("event_src_port", srcPort);
                prepareAttackSource.append("String attackSourcePort = \"").append(srcPort).append("\";\n\t");
            } else {
                templateData.put("event_src_port", "WRONG INPUT");
            }
        }

        if (dstIP.equals("ANY")) {
            prepareAttackDestination.append("String attackDestinationIp = $r.getDestination().getIp();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(dstIP);
            Matcher ipMatcher = ipPattern.matcher(dstIP);

            if (referenceMatcher.find()) {
                templateData.put("event_dst_ip", "WRONG INPUT; CANNOT REFER TO ANYTHING HERE!");
            } else if (ipMatcher.find()) {
                templateData.put("event_dst_ip", dstIP);
                prepareAttackDestination.append("String attackDestinationIp = \"").append(dstIP).append("\";\n\t");
            } else {
                templateData.put("event_dst_ip", "WRONG INPUT");
            }
        }

        if (dstPort.equals("ANY")) {
            prepareAttackDestination.append("String attackDestinationPort = $r.getDestination().getPort();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(dstPort);
            Matcher portMatcher = portPattern.matcher(dstPort);

            if (referenceMatcher.find()) {
                templateData.put("event_dst_port", "WRONG INPUT; CANNOT REFER TO ANYTHING HERE!");
            } else if (portMatcher.find()) {
                templateData.put("event_dst_port", dstPort);
                prepareAttackDestination.append("String attackDestinationPort = \"").append(dstPort).append("\";\n\t");
            } else {
                templateData.put("event_dst_port", "WRONG INPUT");
            }
        }

        prepareAttackSource.append("Host attack_src = new Host(attackSourceIp, attackSourcePort);\n\t");
        prepareAttackDestination.append("Host attack_dst = new Host(attackDestinationIp, attackDestinationPort);\n\t");

        prepareAttackHosts.append(prepareAttackSource).append(prepareAttackDestination);
        templateData.put("hosts_preparation", prepareAttackHosts.toString());
        templateData.put("hasHostsPrep", true);

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, templateData).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public static class ScenarioRuleInitBuilder extends ScenarioRule.ScenarioRuleBuilder {
        public ScenarioRuleInitBuilder(String scenarioId, String scenarioName, int scenarioPriority, String currentRuleRid, int currentRuleLevel, int currentRuleReliability, int currentRuleOccurrence, int currentRuleTimeout, String currentRuleFrom, String currentRuleTo, String currentRulePortFrom, String currentRulePortTo, String currentRulePluginId, String currentRulePluginSid) {
            super(scenarioId, scenarioName, scenarioPriority, currentRuleRid, currentRuleLevel, currentRuleReliability, currentRuleOccurrence, currentRuleTimeout, currentRuleFrom, currentRuleTo, currentRulePortFrom, currentRulePortTo, currentRulePluginId, currentRulePluginSid);
        }

        @Override
        public ScenarioRule build() {
            return new ScenarioRuleInit(this);
        }
    }

}
