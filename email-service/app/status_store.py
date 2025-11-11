notification_status = {}

def set_status(notification_id, status):
    notification_status[notification_id] = status

def get_status(notification_id):
    return notification_status.get(notification_id, "unknown")
