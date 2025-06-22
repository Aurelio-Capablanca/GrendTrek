package com.aib.grendtrek.common;

import java.util.List;

public record ResponseActions<T>(Boolean success, List<T> data, String errorMessage ) {
}
