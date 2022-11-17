package com.qudini.gom.paging;

import lombok.Builder;
import lombok.Value;

/**
 * https://relay.dev/graphql/connections.htm#sec-undefined.PageInfo
 */
@Value
@Builder
public class PageInfo {
    boolean hasPreviousPage;
    boolean hasNextPage;
    String startCursor;
    String endCursor;
}
