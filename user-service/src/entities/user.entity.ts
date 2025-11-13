import {
        Entity,
        PrimaryGeneratedColumn,
        Column,
        OneToOne,
        JoinColumn,
        CreateDateColumn,
        UpdateDateColumn,
} from 'typeorm';
import { UserPreference } from './user-preference.entity';
import * as bcryptjs from 'bcryptjs';

@Entity('users')
export class User {
        @PrimaryGeneratedColumn('uuid')
        id: string;

        @Column({ type: 'varchar', length: 255, name: 'name' })
        name: string;

        @Column({ type: 'varchar', length: 255, unique: true, name: 'email' })
        email: string;

        @Column({ type: 'varchar', length: 255, nullable: true, name: 'push_token' })
        push_token: string;

        @Column({ type: 'varchar', length: 255, name: 'password' })
        password: string;

        @OneToOne(() => UserPreference, (pref) => pref.user, { cascade: true })
        @JoinColumn({ name: 'preferences_id' })
        preferences: UserPreference;

        @CreateDateColumn({ name: 'created_at' })
        created_at: Date;

        @UpdateDateColumn({ name: 'updated_at' })
        updated_at: Date;

        async set_password(password: string): Promise<void> {
                this.password = await bcryptjs.hash(password, 10);
        }

        async validate_password(password: string): Promise<boolean> {
                return bcryptjs.compare(password, this.password);
        }
}