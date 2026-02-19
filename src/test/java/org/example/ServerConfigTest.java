package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServerConfigTest {

    @Test
    void cli_port_wins_over_config_and_default() {
        int port = ServerPortResolver.resolvePort(new String[]{"--port", "80"});
        assertThat(port).isEqualTo(80);
    }

    @Test
    void parsePortFromCli_returns_null_when_args_is_null() {
        Integer port = ServerPortResolver.parsePortFromCli(null);
        assertThat(port).isNull();
    }

    @Test
    void parsePortFromCli_returns_null_when_cli_does_not_contain_port_argument() {
        Integer port = ServerPortResolver.parsePortFromCli(new String[]{"--somethingElse", "123"});
        assertThat(port).isNull();
    }

    @Test
    void parsePortFromCli_throws_when_port_flag_has_no_value() {
        assertThatThrownBy(() -> ServerPortResolver.parsePortFromCli(new String[]{"--port"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing value after --port");
    }

    @Test
    void parsePortFromCli_throws_when_port_value_is_not_a_number() {
        assertThatThrownBy(() -> ServerPortResolver.parsePortFromCli(new String[]{"--port", "abc"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid port value after --port");
    }

    @Test
    void validatePort_throws_when_port_is_out_of_range_low() {
        assertThatThrownBy(() -> ServerPortResolver.validatePort(0, "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Port out of range");
    }

    @Test
    void validatePort_throws_when_port_is_out_of_range_high() {
        assertThatThrownBy(() -> ServerPortResolver.validatePort(70000, "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Port out of range");
    }

}
