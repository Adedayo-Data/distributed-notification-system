import { Injectable, BadRequestException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { UserService } from './user.service';

@Injectable()
export class AuthService {
        constructor(
                private user_service: UserService,
                private jwt_service: JwtService,
        ) { }

        async login(email: string, password: string): Promise<{ access_token: string; user: any }> {
                const user = await this.user_service.validate_user_password(email, password);

                const payload = {
                        sub: user.id,
                        email: user.email,
                        name: user.name,
                };

                const access_token = this.jwt_service.sign(payload);

                return {
                        access_token,
                        user,
                };
        }

        async validate_token(token: string): Promise<any> {
                try {
                        const payload = this.jwt_service.verify(token);
                        return payload;
                } catch (error) {
                        throw new BadRequestException('Invalid token');
                }
        }
}