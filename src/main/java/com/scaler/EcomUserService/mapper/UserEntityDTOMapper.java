package com.scaler.EcomUserService.mapper;

import com.scaler.EcomUserService.dto.UserDto;
import com.scaler.EcomUserService.model.User;

public class UserEntityDTOMapper {
    public static UserDto getUserDTOFromUserEntity(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}