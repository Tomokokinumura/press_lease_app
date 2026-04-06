package com.example.demo.mapper;

import java.util.List;

import com.example.demo.entity.Todo;

public interface TodoMapper {

    List<Todo> findAll();
}
