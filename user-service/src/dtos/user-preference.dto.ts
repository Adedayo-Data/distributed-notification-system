import { IsBoolean } from 'class-validator';

export class UserPreferenceDto {
        @IsBoolean()
        email: boolean;

        @IsBoolean()
        push: boolean;
}