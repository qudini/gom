package com.qudini.gom.paging;

import lombok.Builder;
import lombok.Value;

/**
 * https://relay.dev/graphql/connections.htm#sec-Edge-Types
 */
@Value
@Builder
public class Edge<T> {
    String cursor;
    T node;
}
