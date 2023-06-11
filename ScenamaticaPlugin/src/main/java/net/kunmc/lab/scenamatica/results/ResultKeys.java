package net.kunmc.lab.scenamatica.results;

public class ResultKeys
{
    public static final String KEY_SCENAMATICA = "scenamatica";
    public static final String SCENAMATICA_NAMESPACE = "https://scenamatica.kunlab.org/";
    public static final String SCENAMATICA_NAMESPACE_ID = "scena";
    public static final String SCENAMATICA_PREFIX = SCENAMATICA_NAMESPACE_ID + ":";

    public static final String KEY_SCENAMATICA_VERSION = SCENAMATICA_PREFIX + "version";
    public static final String KEY_SCENAMATICA_BUILD = SCENAMATICA_PREFIX + "build";

    public static final String KEY_PLUGINS = SCENAMATICA_PREFIX + "plugins";
    public static final String KEY_PLUGIN = SCENAMATICA_PREFIX + "plugin";
    public static final String KEY_ID = SCENAMATICA_PREFIX + "id";
    public static final String KEY_PLUGIN_NAME = SCENAMATICA_PREFIX + "name";
    public static final String KEY_PLUGIN_VERSION = SCENAMATICA_PREFIX + "version";
    public static final String KEY_PLUGIN_AUTHORS = SCENAMATICA_PREFIX + "authors";
    public static final String KEY_PLUGIN_AUTHOR = SCENAMATICA_PREFIX + "author";
    public static final String KEY_PLUGIN_DESCRIPTION = SCENAMATICA_PREFIX + "description";
    public static final String KEY_PLUGIN_URL = SCENAMATICA_PREFIX + "url";
    public static final String KEY_PLUGIN_WEBSITE = SCENAMATICA_PREFIX + "website";

    public static final String KEY_TEST_SUITES = "testsuites";
    public static final String KEY_SUITES_TIME = "time";

    public static final String KEY_TEST_SUITE = "testsuite";
    public static final String KEY_SUITE_NAME = "name";
    public static final String KEY_SUITE_TESTS = "tests";
    public static final String KEY_SUITE_FAILURES = "failures";
    public static final String KEY_SUITE_ERRORS = "errors";
    public static final String KEY_SUITE_SKIPPED = "skipped";
    public static final String KEY_SUITE_TIME = "time";
    public static final String KEY_SUITE_ID = "id";

    public static final String KEY_TEST_CASE = "testcase";
    public static final String KEY_CASE_NAME = "name";
    public static final String KEY_CASE_TIME = "time";
    public static final String KEY_CASE_STATUS = "status";

    public static final String KEY_CASE_FAILURE = "failure";
    public static final String KEY_CASE_FAILURE_TYPE = "type";
    public static final String KEY_CASE_FAILURE_MESSAGE = "message";

    public static final String KEY_CASE_SKIPPED = "skipped";

    public static final String KEY_CASE_ERROR = "error";
    public static final String KEY_CASE_ERROR_TYPE = "type";
    public static final String KEY_CASE_ERROR_MESSAGE = "message";

}
