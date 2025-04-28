import path from "path";
import fs from "fs";
// Type for a device
export interface DeviceInfo {
    deviceId: string;
    deviceName: string;
    fileSavePath?: string;
}
import { fileURLToPath } from "url";
import { dirname } from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.join(dirname(__filename), "..");

// Storage file path
const storagePath = path.join(__dirname, "app_db", "devices.json");

// Helper: Read the storage file
export function readDevices(): DeviceInfo[] {
    if (!fs.existsSync(storagePath)) {
        return [];
    }
    const data = fs.readFileSync(storagePath, "utf-8");
    return JSON.parse(data) as DeviceInfo[];
}

// Helper: Write to the storage file
export function writeDevices(data: DeviceInfo[]): void {
    fs.writeFileSync(storagePath, JSON.stringify(data, null, 2), "utf-8");
}
