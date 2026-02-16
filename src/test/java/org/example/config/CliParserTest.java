package org.example.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CliParserTest {

    @Test
    @DisplayName("parse-method should parse CLI inputs correctly")
    void should_parse_commandline_inputs()
    {
        CliOverride override = CliParser.parse(new String[]{"--port", "8080"});

        assertThat(override.port()).isEqualTo(8080);
    }

    @Test
    @DisplayName("missing value should throw IllegalArgumentException")
    void should_throw_exception_if_port_is_not_provided()
    {
        assertThatThrownBy(() -> CliParser.parse(new String[]{"--port"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing value");
    }

    @Test
    @DisplayName("unknown options should throw IllegalArgumentException")
    void  unknown_options_should_throw_exception()
    {
        assertThatThrownBy(() -> CliParser.parse(new String[]{"--ports", "8080"}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("unknown option");
    }

    @Test
    @DisplayName("parsePort out of range should throw NumberFormatException")
    void parsePort_out_of_range_should_throw_exception()
    {
        assertThatThrownBy(() -> CliParser.parsePort("70000"))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("port out of range");

        assertThatThrownBy(() -> CliParser.parsePort("0"))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("port out of range");

        assertThatThrownBy(() -> CliParser.parsePort("-1"))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("port out of range");
    }
}
