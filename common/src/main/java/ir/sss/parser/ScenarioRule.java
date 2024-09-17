package ir.sss.parser;

import java.util.regex.Pattern;

public abstract class ScenarioRule {

    private ScenarioParams scenarioParams;
    private RuleParams currentRuleParams;
    private RuleParams previousRuleParams;

    protected static Pattern referencePattern = Pattern.compile("(\\d+):(SRC_IP|SRC_PORT|DST_IP|DST_PORT)", Pattern.CASE_INSENSITIVE);
    protected static Pattern ipPattern = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$", Pattern.CASE_INSENSITIVE);
    protected static Pattern portPattern = Pattern.compile("^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$", Pattern.CASE_INSENSITIVE);


    public ScenarioRule(ScenarioRuleBuilder scenarioRuleBuilder) {
        this.scenarioParams = scenarioRuleBuilder.scenarioParams;
        this.currentRuleParams = scenarioRuleBuilder.currentRuleParams;
        this.previousRuleParams = scenarioRuleBuilder.previousRuleParams;
    }

    public abstract String getRule();

    public ScenarioParams getScenarioParams() {
        return scenarioParams;
    }

    public RuleParams getCurrentRuleParams() {
        return currentRuleParams;
    }

    public RuleParams getPreviousRuleParams() {
        return previousRuleParams;
    }

    public static class ScenarioParams {

        public String id;
        public String name;
        public int priority;

        public ScenarioParams(String id, String name, int priority) {
            this.id = id;
            this.name = name;
            this.priority = priority;
        }
    }

    public static class RuleParams {

        public String rid;
        public int level;
        public int reliability;
        public int occurrence;
        public int timeout;
        public String from;
        public String to;
        public String portFrom;
        public String portTo;
        public String pluginId;
        public String pluginSid;

        public RuleParams(String rid, int level, int reliability, int occurrence, int timeout, String from, String to, String portFrom, String portTo, String pluginId, String pluginSid) {
            this.rid = rid;
            this.level = level;
            this.reliability = reliability;
            this.occurrence = occurrence;
            this.timeout = timeout;
            this.from = from;
            this.to = to;
            this.portFrom = portFrom;
            this.portTo = portTo;
            this.pluginId = pluginId;
            this.pluginSid = pluginSid;
        }
    }

    public abstract static class ScenarioRuleBuilder {

        private ScenarioParams scenarioParams;
        private RuleParams currentRuleParams;
        private RuleParams previousRuleParams;

        public ScenarioRuleBuilder(String scenarioId, String scenarioName, int scenarioPriority, String currentRuleRid, int currentRuleLevel, int currentRuleReliability, int currentRuleOccurrence, int currentRuleTimeout, String currentRuleFrom, String currentRuleTo, String currentRulePortFrom, String currentRulePortTo, String currentRulePluginId, String currentRulePluginSid) {
            this.scenarioParams = new ScenarioParams(scenarioId, scenarioName, scenarioPriority);
            this.currentRuleParams = new RuleParams(currentRuleRid, currentRuleLevel, currentRuleReliability, currentRuleOccurrence, currentRuleTimeout, currentRuleFrom, currentRuleTo, currentRulePortFrom, currentRulePortTo, currentRulePluginId, currentRulePluginSid);
            this.previousRuleParams = null;
        }

        public ScenarioRuleBuilder setPreviousRule(String previousRuleRid, int previousRuleLevel, int previousRuleReliability, int previousRuleOccurrence, int previousRuleTimeout, String previousRuleFrom, String previousRuleTo, String previousRulePortFrom, String previousRulePortTo, String previousRulePluginId, String previousRulePluginSid) {
            this.previousRuleParams = new RuleParams(previousRuleRid, previousRuleLevel, previousRuleReliability, previousRuleOccurrence, previousRuleTimeout, previousRuleFrom, previousRuleTo, previousRulePortFrom, previousRulePortTo, previousRulePluginId, previousRulePluginSid);
            return this;
        }

        public abstract ScenarioRule build();
    }
}
