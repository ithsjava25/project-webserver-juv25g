package org.example.config;

public final class CliParser {
    public static CliOverride parse(String[] args) {
        Integer port = null;
        String rootDir = null;
        String logLevel = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Invalid argument: " + arg + " --help for list of valid commands");
            }
            String key = arg.substring(2);

            if (key.equals("help")) {
                IO.println(printHelp());
                return CliOverride.empty();
            }

            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new IllegalArgumentException("missing value for '--" + key + "' (see --help)");
            }

            String value = args[++i];

            switch (key) {
                case "port" -> port = parsePort(value);
                case "rootDir" -> rootDir = value;
                case "logLevel" -> logLevel = value;
                default -> throw new IllegalArgumentException("unknown option '--" + key + "' (see --help)");
            }
        }
        return new CliOverride(port, rootDir, logLevel);
    }

    public static int parsePort(String input) {
        int p;
        try {
            p = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("invalid port: '" + input + "' is not a valid number");
        }
        if (p < 1 || p > 65535) {
            throw new NumberFormatException("port out of range: " + p + ". Must be between 1 and 65535");
        }
        return p;
    }

    public static String printHelp() {
        return """
                usage:
                  --port <1-65535>
                  --rootDir <path>
                  --logLevel <level>
                  --help
                
                examples:
                  --port 8080 --rootDir ./www --logLevel INFO
                """;
    }
}
