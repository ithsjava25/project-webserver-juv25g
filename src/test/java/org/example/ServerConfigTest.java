package org.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerConfigTest {

    @Test
    void cli_port_wins_over_default() {
        int port = ServerPortResolver.resolvePort(new String[]{"--port", "80"});
        assertThat(port).isEqualTo(80);
    }
}
