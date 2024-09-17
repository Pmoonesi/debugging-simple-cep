package ir.sss.parser;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import ir.sss.model.Host;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioRuleLevelUp extends ScenarioRule{

    public ScenarioRuleLevelUp(ScenarioRuleBuilder scenarioRuleBuilder) {
        super(scenarioRuleBuilder);
    }

    @Override
    public String getRule() {
        String templateString =
                """
                        rule "Attack {{scenario_id}} - Level {{attack_level}} - ID {{attack_rid}}"
                        when
                            $prevLevel: Attack(sid == {{scenario_id}}, rid == {{prev_attack_rid}})
                            $scenario: Scenario(rid == {{prev_attack_rid}}, attackList contains $prevLevel)
                            $eventsList : ArrayList(size >= {{attack_occurrences}}) from accumulate(
                                    $a : RequestEvent(\
                        {{#hasSrcIP}}sourceIp == {{event_src_ip}}, {{/hasSrcIP}}\
                        {{#hasSrcPort}}sourcePort == {{event_src_port}}, {{/hasSrcPort}}\
                        {{#hasDstIP}}destinationIp == {{event_dst_ip}}, {{/hasDstIP}}\
                        {{#hasDstPort}}destinationPort == {{event_dst_port}}, {{/hasDstPort}}\
                        pluginId == {{event_plugin_id}}, pluginSid == {{event_plugin_sid}}{{#hasTimeOut}}, this after[1ms, {{attack_time_out}}s] $prevLevel{{/hasTimeOut}}),
                                    init( ArrayList list = new ArrayList(); ),
                                    action( list.add($a); ),
                                    result( list )
                            )
                        then
                            {{#hasHostsPrep}}{{{hosts_preparation}}}{{/hasHostsPrep}}
                            Attack bfa = new Attack({{scenario_id}}, {{attack_rid}}, $scenario{{#hasHostsPrep}}, attack_src, attack_dst{{/hasHostsPrep}});
                            bfa.setEvents($eventsList.subList(0,{{attack_occurrences}}));
                            insert(bfa);
                            $scenario.addAttack(bfa);
                            update($scenario);
                        end""";

        // Prepare the data map
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("attack_level", getCurrentRuleParams().level);
        templateData.put("attack_rid", getCurrentRuleParams().rid);
        templateData.put("prev_attack_level", getPreviousRuleParams().level);
        templateData.put("prev_attack_rid", getPreviousRuleParams().rid);
        templateData.put("attack_occurrences", getCurrentRuleParams().occurrence);
        templateData.put("event_plugin_id", getCurrentRuleParams().pluginId);
        templateData.put("event_plugin_sid", getCurrentRuleParams().pluginSid);
        templateData.put("attack_time_out", getCurrentRuleParams().timeout);
        templateData.put("scenario_id", getScenarioParams().id);

        templateData.put("hasTimeOut", getCurrentRuleParams().timeout != 0);

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
            prepareAttackSource.append("String attackSourceIp = ((RequestEvent) $eventsList.getFirst()).getSource().getIp();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(srcIP);
            Matcher ipMatcher = ipPattern.matcher(srcIP);

            if (referenceMatcher.find()) {
                templateData.put("event_src_ip", "$scenario.getAttack(" + ( Integer.valueOf(referenceMatcher.group(1)) - 1) + ").sourceIp");
                prepareAttackSource.append("String attackSourceIp = ").append("$scenario.getAttack(").append(Integer.valueOf(referenceMatcher.group(1)) - 1).append(").getSourceIp();\n\t");
            } else if (ipMatcher.find()) {
                templateData.put("event_src_ip", srcIP);
                prepareAttackSource.append("String attackSourceIp = \"").append(srcIP).append("\";\n\t");
            } else {
                templateData.put("event_src_ip", "WRONG INPUT");
            }
        }

        if (srcPort.equals("ANY")) {
            prepareAttackSource.append("String attackSourcePort = ((RequestEvent) $eventsList.getFirst()).getSource().getPort();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(srcPort);
            Matcher portMatcher = portPattern.matcher(srcPort);

            if (referenceMatcher.find()) {
                templateData.put("event_src_port", "$scenario.getAttack(" + ( Integer.valueOf(referenceMatcher.group(1)) - 1) + ").sourcePort");
                prepareAttackSource.append("String attackSourcePort = ").append("$scenario.getAttack(").append(Integer.valueOf(referenceMatcher.group(1)) - 1).append(").getSourcePort();\n\t");
            } else if (portMatcher.find()) {
                templateData.put("event_src_port", srcPort);
                prepareAttackSource.append("String attackSourcePort = \"").append(srcPort).append("\";\n\t");
            } else {
                templateData.put("event_src_port", "WRONG INPUT");
            }
        }

        if (dstIP.equals("ANY")) {
            prepareAttackDestination.append("String attackDestinationIp = ((RequestEvent) $eventsList.getFirst()).getDestination().getIp();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(dstIP);
            Matcher ipMatcher = ipPattern.matcher(dstIP);

            if (referenceMatcher.find()) {
                templateData.put("event_dst_ip", "$scenario.getAttack(" + ( Integer.valueOf(referenceMatcher.group(1)) - 1) + ").destinationIp");
                prepareAttackDestination.append("String attackDestinationIp = ").append("$scenario.getAttack(").append(Integer.valueOf(referenceMatcher.group(1)) - 1).append(").getDestinationIp();\n\t");
            } else if (ipMatcher.find()) {
                templateData.put("event_dst_ip", dstIP);
                prepareAttackDestination.append("String attackDestinationIp = \"").append(dstIP).append("\";\n\t");
            } else {
                templateData.put("event_dst_ip", "WRONG INPUT");
            }
        }

        if (dstPort.equals("ANY")) {
            prepareAttackDestination.append("String attackDestinationPort = ((RequestEvent) $eventsList.getFirst()).getDestination().getPort();\n\t");
        } else {
            Matcher referenceMatcher = referencePattern.matcher(dstPort);
            Matcher portMatcher = portPattern.matcher(dstPort);

            if (referenceMatcher.find()) {
                templateData.put("event_dst_port", "$scenario.getAttack(" + ( Integer.valueOf(referenceMatcher.group(1)) - 1) + ").destinationPort");
                prepareAttackSource.append("String attackDestinationPort = ").append("$scenario.getAttack(").append(Integer.valueOf(referenceMatcher.group(1)) - 1).append(").getDestinationPort();\n\t");
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

        // Compile and render the template
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(templateString), "ruleTemplate");

        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, templateData).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public static class ScenarioRuleLevelUpBuilder extends ScenarioRule.ScenarioRuleBuilder {
        public ScenarioRuleLevelUpBuilder(String scenarioId, String scenarioName, int scenarioPriority, String currentRuleRid, int currentRuleLevel, int currentRuleReliability, int currentRuleOccurrence, int currentRuleTimeout, String currentRuleFrom, String currentRuleTo, String currentRulePortFrom, String currentRulePortTo, String currentRulePluginId, String currentRulePluginSid) {
            super(scenarioId, scenarioName, scenarioPriority, currentRuleRid, currentRuleLevel, currentRuleReliability, currentRuleOccurrence, currentRuleTimeout, currentRuleFrom, currentRuleTo, currentRulePortFrom, currentRulePortTo, currentRulePluginId, currentRulePluginSid);
        }

        @Override
        public ScenarioRule build() {
            return new ScenarioRuleLevelUp(this);
        }
    }
}
