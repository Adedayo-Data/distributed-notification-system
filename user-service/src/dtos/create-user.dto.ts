import {
        IsEmail,
        IsString,
        IsNotEmpty,
        MinLength,
        IsOptional,
} from 'class-validator';
import { UserPreferenceDto } from './user-preference.dto';

export class CreateUserDto {
        @IsString()
        @IsNotEmpty()
        name: string;

        @IsEmail()
        email: string;

        @IsString()
        @MinLength(8)
        password: string;

        @IsOptional()
        @IsString()
        push_token?: string;

        @IsNotEmpty()
        preferences: UserPreferenceDto;
}