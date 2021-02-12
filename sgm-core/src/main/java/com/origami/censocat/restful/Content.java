/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.restful;

/**
 *
 * @author ANGEL NAVARRO
 */
public class Content<T> {

    private final boolean isSuccess;
    private final T data;
    private final ApiError error;

    private Content(final boolean isSuccess, final T data, final ApiError error) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.error = error;
        System.out.println("///// Data " + this.data + " Error " + this.error);
    }

    static <T> Content<T> success(final T data) {
        return new Content<>(true, data, null);
    }

    static <T> Content<T> error(final ApiError error) {
        return new Content<>(false, null, error);
    }

    static <T> Content<T> error1(final Object data) {
        System.out.println("Error " + data);
        return new Content<>(false, (T) data, null);
    }

    boolean isSuccess() {
        return isSuccess;
    }

    T getData()
            throws IllegalStateException {
        if (!isSuccess) {
            throw new IllegalStateException();
        }
        return data;
    }

    ApiError getError()
            throws IllegalStateException {
        if (isSuccess) {
            throw new IllegalStateException();
        }
        return error;
    }

}
