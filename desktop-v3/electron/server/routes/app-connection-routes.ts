import { Request, Response, Router } from "express";
import { DeviceInfo, readDevices, writeDevices } from "../../db";
import { sendConnectedDevice } from "../../events/device-events";

const router = Router();

router.post("/get", (req: Request, res: Response): any => {
    try {
        const body = req.body;
        if (!body?.deviceId) {
            return res.status(400).json({
                message: "Invalid Info",
            });
        }
        const { deviceId } = (req.body as DeviceInfo) || {};

        const devices = readDevices();

        const found = devices.find((device) => device.deviceId === deviceId);

        if (found) {
            res.json({ connected: true });
        } else {
            res.json({ connected: false });
        }
    } catch (err) {
        console.log("Err", err);
        return res.status(404).json({
            message: "Server Error Found",
        });
    }
});

router.post("/set", (req: Request, res: Response): any => {
    try {
        const body = req.body;
        if (!body?.deviceId || !body?.deviceName) {
            return res.status(400).json({
                message: "Invalid Info",
                connected: false,
            });
        }
        const { deviceId, deviceName } = req.body as DeviceInfo;
        let devices = readDevices();

        // Check if already exists
        const exists = devices.some(
            (device) =>
                device.deviceId === deviceId && device.deviceName === deviceName
        );

        if (!exists) {
            devices.push({ deviceId, deviceName });
            writeDevices(devices);
            sendConnectedDevice();
            res.json({ connected: true, message: "Device saved" });
        } else {
            res.json({ connected: true, message: "Device already exists" });
        }
    } catch (err) {
        console.log("Err", err);
        return res.status(404).json({
            message: "Server Error Found",
        });
    }
});

export default router;
