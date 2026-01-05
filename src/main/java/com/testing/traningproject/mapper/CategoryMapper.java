package com.testing.traningproject.mapper;

import com.testing.traningproject.model.dto.response.CategoryResponse;
import com.testing.traningproject.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper for Category entity
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "totalServices", ignore = true) // Set manually in service
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}

