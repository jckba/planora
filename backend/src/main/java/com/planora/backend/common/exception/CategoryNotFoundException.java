package com.planora.backend.common.exception;

public class CategoryNotFoundException extends ResourceNotFoundException
{
    public CategoryNotFoundException() {
        super("Category not found");
    }
}
