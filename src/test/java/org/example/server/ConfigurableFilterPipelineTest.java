package org.example.server;

import org.example.filter.Filter;
import org.example.filter.FilterChain;
import org.example.httpparser.HttpRequest;
import org.example.http.HttpResponseBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurableFilterPipelineTest {

    @Test
    void global_filter_runs() {

        List<String> events = new ArrayList<>();

        Filter filter = new TestFilter("g1", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(filter, 1, null)
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/home"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(
                List.of("g1", "handler"),
                events
        );
    }

    @Test
    void filter_can_stop_chain() {

        List<String> events = new ArrayList<>();

        Filter stopFilter = new TestFilter("stop", events, true);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(stopFilter, 1, null)
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        HttpResponseBuilder response = pipeline.execute(
                newRequest("/home"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(HttpResponseBuilder.SC_FORBIDDEN, response.getStatusCode());
        assertEquals(List.of("stop"), events);
    }

    @Test
    void route_specific_filter_runs_when_path_matches() {
        List<String> events = new ArrayList<>();

        Filter routeFilter = new TestFilter("r1", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(routeFilter, 1, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/api/users"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(List.of("r1", "handler"), events);
    }

    @Test
    void route_specific_filter_is_skipped_when_path_does_not_match() {
        List<String> events = new ArrayList<>();

        Filter routeFilter = new TestFilter("r1", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(routeFilter, 1, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/public"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(List.of("handler"), events);
    }

    @Test
    void mixed_pipeline_runs_global_then_route_then_handler() {
        List<String> events = new ArrayList<>();

        Filter global = new TestFilter("g1", events, false);
        Filter route = new TestFilter("r1", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(global, 1, null),
                new FilterRegistration(route, 1, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/api/users"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(List.of("g1", "r1", "handler"), events);
    }

    @Test
    void ordering_is_by_order_field() {
        List<String> events = new ArrayList<>();

        Filter f20 = new TestFilter("f20", events, false);
        Filter f10 = new TestFilter("f10", events, false);
        Filter f30 = new TestFilter("f30", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(f20, 20, null),
                new FilterRegistration(f10, 10, null),
                new FilterRegistration(f30, 30, null)
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/home"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(List.of("f10", "f20", "f30", "handler"), events);
    }

    @Test
    void global_stop_filter_prevents_route_and_handler() {
        List<String> events = new ArrayList<>();

        Filter globalStop = new TestFilter("gStop", events, true);
        Filter routeFilter = new TestFilter("r1", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(globalStop, 1, null),
                new FilterRegistration(routeFilter, 1, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        HttpResponseBuilder response = pipeline.execute(
                newRequest("/api/users"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(HttpResponseBuilder.SC_FORBIDDEN, response.getStatusCode());
        assertEquals(List.of("gStop"), events);
    }

    @Test
    void route_stop_filter_prevents_handler_but_global_runs() {
        List<String> events = new ArrayList<>();

        Filter global = new TestFilter("g1", events, false);
        Filter routeStop = new TestFilter("rStop", events, true);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(global, 1, null),
                new FilterRegistration(routeStop, 1, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        HttpResponseBuilder response = pipeline.execute(
                newRequest("/api/users"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(HttpResponseBuilder.SC_FORBIDDEN, response.getStatusCode());
        assertEquals(List.of("g1", "rStop"), events);
    }

    @Test
    void response_phase_can_be_done_with_try_finally_in_filters_reverse_order() {
        List<String> events = new ArrayList<>();

        Filter f1 = new Filter() {
            @Override
            public void init() {
                // no-op
            }

            @Override
            public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
                events.add("f1:enter");
                try {
                    chain.doFilter(request, response);
                } finally {
                    events.add("f1:exit");
                }
            }

            @Override
            public void destroy() {
                // no-op
            }
        };

        Filter f2 = new Filter() {
            @Override
            public void init() {
                // no-op
            }

            @Override
            public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {
                events.add("f2:enter");
                try {
                    chain.doFilter(request, response);
                } finally {
                    events.add("f2:exit");
                }
            }

            @Override
            public void destroy() {
                // no-op
            }
        };

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(f1, 10, null),
                new FilterRegistration(f2, 20, null)
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/home"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(
                List.of("f1:enter", "f2:enter", "handler", "f2:exit", "f1:exit"),
                events
        );
    }

    @Test
    void global_filters_run_before_route_filters_even_if_route_has_lower_order() {
        List<String> events = new ArrayList<>();

        Filter global100 = new TestFilter("g100", events, false);
        Filter route0 = new TestFilter("r0", events, false);

        List<FilterRegistration> regs = List.of(
                new FilterRegistration(global100, 100, null),
                new FilterRegistration(route0, 0, List.of("/api/*"))
        );

        ConfigurableFilterPipeline pipeline =
                new ConfigurableFilterPipeline(regs);

        pipeline.execute(
                newRequest("/api/users"),
                (req, resp) -> events.add("handler")
        );

        assertEquals(List.of("g100", "r0", "handler"), events);
    }

    private static HttpRequest newRequest(String path) {
        return new HttpRequest("GET", path, "HTTP/1.1", Map.of(), "");
    }

    static class TestFilter implements Filter {

        private final String name;
        private final List<String> events;
        private final boolean stop;

        TestFilter(String name, List<String> events, boolean stop) {
            this.name = name;
            this.events = events;
            this.stop = stop;
        }

        @Override
        public void init() {
            // no-op
        }
        @Override
        public void doFilter(HttpRequest request, HttpResponseBuilder response, FilterChain chain) {

            events.add(name);

            if (stop) {
                response.setStatusCode(HttpResponseBuilder.SC_FORBIDDEN);
                return;
            }

            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
            // no-op
        }
    }
}
