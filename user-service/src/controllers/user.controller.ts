import {
        Controller,
        Post,
        Get,
        Put,
        Delete,
        Body,
        Param,
        Query,
} from '@nestjs/common';
import { UserService } from '../services/user.service';
import { CreateUserDto } from '../dtos/create-user.dto';
import { UpdateUserDto } from '../dtos/update-user.dto';

@Controller('api/v1/users')
export class UserController {
        constructor(private user_service: UserService) { }

        @Post()
        async create_user(@Body() create_user_dto: CreateUserDto) {
                try {
                        const user = await this.user_service.create_user(create_user_dto);
                        return {
                                success: true,
                                data: user,
                                message: 'User created successfully',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to create user',
                                meta: {},
                        };
                }
        }

        @Get()
        async get_all_users(@Query('page') page: number = 1, @Query('limit') limit: number = 10) {
                try {
                        const result = await this.user_service.get_all_users(page, limit);
                        return {
                                success: true,
                                data: result.users,
                                message: 'Users retrieved successfully',
                                meta: result.meta,
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to retrieve users',
                                meta: {},
                        };
                }
        }

        @Get(':user_id')
        async get_user_by_id(@Param('user_id') user_id: string) {
                try {
                        const user = await this.user_service.get_user_by_id(user_id);
                        return {
                                success: true,
                                data: user,
                                message: 'User retrieved successfully',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to retrieve user',
                                meta: {},
                        };
                }
        }

        @Put(':user_id')
        async update_user(
                @Param('user_id') user_id: string,
                @Body() update_user_dto: UpdateUserDto,
        ) {
                try {
                        const user = await this.user_service.update_user(user_id, update_user_dto);
                        return {
                                success: true,
                                data: user,
                                message: 'User updated successfully',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to update user',
                                meta: {},
                        };
                }
        }

        @Put(':user_id/push-token')
        async update_push_token(
                @Param('user_id') user_id: string,
                @Body('push_token') push_token: string,
        ) {
                try {
                        const user = await this.user_service.update_push_token(user_id, push_token);
                        return {
                                success: true,
                                data: user,
                                message: 'Push token updated successfully',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to update push token',
                                meta: {},
                        };
                }
        }

        @Post('validate')
        async validate_user_password(
                @Body('email') email: string,
                @Body('password') password: string,
        ) {
                try {
                        const user = await this.user_service.validate_user_password(email, password);
                        return {
                                success: true,
                                data: user,
                                message: 'User validated successfully',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Validation failed',
                                meta: {},
                        };
                }
        }

        @Delete(':user_id')
        async delete_user(@Param('user_id') user_id: string) {
                try {
                        const result = await this.user_service.delete_user(user_id);
                        return {
                                success: true,
                                data: result,
                                message: result.message,
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Failed to delete user',
                                meta: {},
                        };
                }
        }
}