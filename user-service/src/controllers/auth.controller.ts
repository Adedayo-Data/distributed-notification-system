import { Controller, Post, Body } from '@nestjs/common';
import { AuthService } from '../services/auth.service';

@Controller('api/v1/auth')
export class AuthController {
        constructor(private auth_service: AuthService) { }

        @Post('login')
        async login(@Body('email') email: string, @Body('password') password: string) {
                try {
                        const result = await this.auth_service.login(email, password);
                        return {
                                success: true,
                                data: result,
                                message: 'Login successful',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Login failed',
                                meta: {},
                        };
                }
        }

        @Post('verify-token')
        async verify_token(@Body('token') token: string) {
                try {
                        const payload = await this.auth_service.validate_token(token);
                        return {
                                success: true,
                                data: payload,
                                message: 'Token is valid',
                                meta: {},
                        };
                } catch (error) {
                        return {
                                success: false,
                                error: error.message,
                                message: 'Invalid token',
                                meta: {},
                        };
                }
        }
}