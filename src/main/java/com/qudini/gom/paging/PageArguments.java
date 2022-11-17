package com.qudini.gom.paging;

import java.util.Optional;

/**
 * https://relay.dev/graphql/connections.htm#sec-Arguments
 */
public interface PageArguments {

    int getFirst();

    Optional<String> getAfter();

}
