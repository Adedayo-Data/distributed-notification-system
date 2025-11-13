import { Controller, Get } from '@nestjs/common';

@Controller('health')
export class HealthController {
        @Get()
        health_check() {
                return {
                        status: 'UP',
                        service: 'user-service',
                        timestamp: new Date().toISOString(),
                };
        }
}