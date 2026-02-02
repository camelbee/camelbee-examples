package com.mycompany.config;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;

/**
 * Shared mapStruct mapper configuration.
 */
@MapperConfig(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SharedMapperConfig {

}
