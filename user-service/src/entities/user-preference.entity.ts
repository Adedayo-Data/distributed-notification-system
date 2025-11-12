import { Entity, PrimaryGeneratedColumn, Column, OneToOne } from 'typeorm';
import { User } from './user.entity';

@Entity('user_preferences')
export class UserPreference {
        @PrimaryGeneratedColumn('uuid')
        id: string;

        @OneToOne(() => User, (user) => user.preferences)
        user: User;

        @Column({ type: 'boolean', default: true, name: 'email_notifications' })
        email_notifications: boolean;

        @Column({ type: 'boolean', default: true, name: 'push_notifications' })
        push_notifications: boolean;

        @Column({ type: 'timestamp', default: () => 'CURRENT_TIMESTAMP', name: 'created_at' })
        created_at: Date;

        @Column({ type: 'timestamp', onUpdate: 'CURRENT_TIMESTAMP', name: 'updated_at' })
        updated_at: Date;
}