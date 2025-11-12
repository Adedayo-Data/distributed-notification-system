import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../entities/user.entity';
import { UserPreference } from '../entities/user-preference.entity';
import { CreateUserDto } from '../dtos/create-user.dto';
import { UpdateUserDto } from '../dtos/update-user.dto';

@Injectable()
export class UserService {
        constructor(
                @InjectRepository(User)
                private user_repository: Repository<User>,
                @InjectRepository(UserPreference)
                private preference_repository: Repository<UserPreference>,
        ) { }

        async create_user(create_user_dto: CreateUserDto): Promise<User> {
                const existing_user = await this.user_repository.findOne({
                        where: { email: create_user_dto.email },
                });

                if (existing_user) {
                        throw new BadRequestException('Email already registered');
                }

                const user = new User();
                user.name = create_user_dto.name;
                user.email = create_user_dto.email;
                user.push_token = create_user_dto.push_token || '';

                await user.set_password(create_user_dto.password);

                const preferences = new UserPreference();
                preferences.email_notifications = create_user_dto.preferences.email;
                preferences.push_notifications = create_user_dto.preferences.push;

                user.preferences = preferences;

                return await this.user_repository.save(user);
        }

        async get_user_by_id(user_id: string): Promise<User> {
                const user = await this.user_repository.findOne({
                        where: { id: user_id },
                        relations: ['preferences'],
                });

                if (!user) {
                        throw new NotFoundException(`User with ID ${user_id} not found`);
                }

                return user;
        }

        async get_user_by_email(email: string): Promise<User> {
                const user = await this.user_repository.findOne({
                        where: { email },
                        relations: ['preferences'],
                });

                if (!user) {
                        throw new NotFoundException(`User with email ${email} not found`);
                }

                return user;
        }

        async update_user(user_id: string, update_user_dto: UpdateUserDto): Promise<User> {
                const user = await this.get_user_by_id(user_id);

                if (update_user_dto.name) {
                        user.name = update_user_dto.name;
                }

                if (update_user_dto.email) {
                        const existing_user = await this.user_repository.findOne({
                                where: { email: update_user_dto.email },
                        });

                        if (existing_user && existing_user.id !== user_id) {
                                throw new BadRequestException('Email already in use');
                        }

                        user.email = update_user_dto.email;
                }

                if (update_user_dto.push_token) {
                        user.push_token = update_user_dto.push_token;
                }

                if (update_user_dto.preferences) {
                        const preferences = user.preferences || new UserPreference();
                        preferences.email_notifications = update_user_dto.preferences.email;
                        preferences.push_notifications = update_user_dto.preferences.push;
                        user.preferences = preferences;
                }

                return await this.user_repository.save(user);
        }

        async update_push_token(user_id: string, push_token: string): Promise<User> {
                const user = await this.get_user_by_id(user_id);
                user.push_token = push_token;
                return await this.user_repository.save(user);
        }

        async get_all_users(page: number = 1, limit: number = 10): Promise<{ users: User[]; meta: any }> {
                const skip = (page - 1) * limit;
                const [users, total] = await this.user_repository.findAndCount({
                        relations: ['preferences'],
                        skip,
                        take: limit,
                });

                return {
                        users,
                        meta: {
                                total,
                                limit,
                                page,
                                total_pages: Math.ceil(total / limit),
                                has_next: page < Math.ceil(total / limit),
                                has_previous: page > 1,
                        },
                };
        }

        async validate_user_password(email: string, password: string): Promise<User> {
                const user = await this.get_user_by_email(email);
                const is_valid = await user.validate_password(password);

                if (!is_valid) {
                        throw new BadRequestException('Invalid credentials');
                }

                return user;
        }

        async delete_user(user_id: string): Promise<{ message: string }> {
                const user = await this.get_user_by_id(user_id);
                await this.user_repository.remove(user);
                return { message: 'User deleted successfully' };
        }
}