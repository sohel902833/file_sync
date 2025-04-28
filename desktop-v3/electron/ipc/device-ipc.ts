import { BrowserWindow, dialog, IpcMain } from "electron";
import { readDevices, writeDevices } from "../db";
import { sendConnectedDevice } from "../events/device-events";

export const registerDeviceIpc = (ipc: IpcMain, _: BrowserWindow | null) => {
    ipc.removeHandler("choose_folder_for_device");
    ipc.handle("choose_folder_for_device", async (_, receivedData) => {
        const result = await dialog.showOpenDialog({
            properties: ["openDirectory"],
        });

        if (result.canceled || result.filePaths.length === 0) {
            return null;
        }

        const filePath = result.filePaths[0];
        let deviceList = [...readDevices()];
        if (deviceList?.length) {
            console.log("Device List", deviceList);
            deviceList = deviceList?.map((item) => {
                if (item?.deviceId === receivedData?.deviceId) {
                    return {
                        ...item,
                        fileSavePath: filePath,
                    };
                }
                return item;
            });
            writeDevices(deviceList);
            sendConnectedDevice();
        }
        return filePath;
    });
};
