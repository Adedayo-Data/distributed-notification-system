import { IsEmail, IsString, IsOptional } from 'class-validator';
import { UserPreferenceDto } from './user-preference.dto';

export class UpdateUserDto {
        @IsOptional()
        @IsString()
        name?: string;

        @IsOptional()
        @IsEmail()
        email?: string;

        @IsOptional()
        @IsString()
        push_token?: string;

        @IsOptional()
        preferences?: UserPreferenceDto;
}