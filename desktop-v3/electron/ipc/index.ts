import { BrowserWindow, IpcMain } from "electron";
import { registerDeviceIpc } from "./device-ipc";

export function registerAllIpcHandlers(
    ipcMain: IpcMain,
    mainWindow: BrowserWindow | null
) {
    registerDeviceIpc(ipcMain, mainWindow);
    // Future handlers can be added here easily
}
