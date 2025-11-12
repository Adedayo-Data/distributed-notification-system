import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { UserPreference } from './entities/user-preference.entity';
import { UserService } from './services/user.service';
import { UserController } from './controllers/user.controller';
import { HealthController } from './health/health.controller';

@Module({
        imports: [
                ConfigModule.forRoot({
                        isGlobal: true,
                        envFilePath: '.env',
                }),
                TypeOrmModule.forRoot({
                        type: 'postgres',
                        host: process.env.DB_HOST || 'localhost',
                        port: parseInt(process.env.DB_PORT || '5432') || 5432,
                        username: process.env.DB_USERNAME || 'postgres',
                        password: process.env.DB_PASSWORD || 'password',
                        database: process.env.DB_NAME || 'notification_db',
                        entities: [User, UserPreference],
                        synchronize: process.env.NODE_ENV !== 'production',
                        logging: process.env.NODE_ENV !== 'production',
                }),
                TypeOrmModule.forFeature([User, UserPreference]),
        ],
        controllers: [UserController, HealthController],
        providers: [UserService],
})
export class AppModule { }