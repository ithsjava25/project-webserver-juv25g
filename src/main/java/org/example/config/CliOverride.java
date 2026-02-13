package org.example.config;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CliOverride {
    private Integer port;
    private String rootDir;
    private String logLevel;

    public CliOverride parse (String[] args)  {
        CliOverride override = new CliOverride();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Invalid argument: " + arg + " --help for list of valid commands");
            }
            String key = arg.substring(2);

            if (key.equals("help")) {
                IO.println(printHelp());
            }

            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new IllegalArgumentException("missing value for '--" + key + "' (see --help)");
            }

            String value = args[++i];

            switch (key) {
                case "port" -> override.port = parsePort(value);
                case "rootDir" -> override.rootDir = value;
                case "logLevel" -> override.logLevel = value;
                default -> throw new IllegalArgumentException("unknown option '--" + key + "' (see --help)");
            }
        }
        return override;
    }

    public static int parsePort  (String input) {
        try {
            int p = Integer.parseInt(input);
            if (p < 1 || p > 65535) {
                throw new NumberFormatException("port must be between 1 and 65535");
            }
            return p;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("port must be a number between 1 and 65535");
        }
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
