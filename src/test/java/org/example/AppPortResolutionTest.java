package org.example;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppPortResolutionTest {

    @Test
    void cli_port_wins_over_config() {
        int port = App.resolvePort(new String[]{"--port", "8000"}, 9090);
        assertThat(port).isEqualTo(8000);
    }

    @Test
    void config_port_used_when_no_cli_arg() {
        int port = App.resolvePort(new String[]{}, 9090);
        assertThat(port).isEqualTo(9090);
    }
}