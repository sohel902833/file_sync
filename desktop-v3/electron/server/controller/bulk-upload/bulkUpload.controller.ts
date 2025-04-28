import { Request, Response, NextFunction } from "express";
import multer from "multer";
import { createStorage } from "./bulkUpload.utils";
import { createError } from "../../errors";
import { DeviceInfo, readDevices } from "../../../db";
import path from "path";
import { app } from "electron";
import fs from "fs/promises";

export const getStorageFolderPath = (device: DeviceInfo) => {
    if (device?.fileSavePath) {
        return path.join(
            device?.fileSavePath,
            `${device?.deviceName}-${device?.deviceId}`
        );
    } else {
        const documentPath = app.getPath("pictures");
        return path.join(
            documentPath,
            "AuthSync",
            `${device?.deviceName}-${device?.deviceId}`
        );
    }
};

async function getUploadedFileInfo(filePath: string) {
    try {
        const stats = await fs.stat(filePath);
        return {
            updatedAt: stats.mtime,
            createdAt: stats.birthtime,
            size: stats.size,
        };
    } catch (err) {
        console.error("Error getting file info:", err);
        return null;
    }
}

export const uploadFiles = (
    max_length: number,
    field_name: string,
    imageRequired: boolean = true
): any => {
    return async (req: Request, res: Response, next: NextFunction) => {
        try {
            const deviceId = req?.params?.deviceId;
            let folderName = req.params?.folderName;
            if (!folderName || folderName === "none") {
                folderName = "";
            }
            const deviceList = readDevices();
            const currentDevice = deviceList?.find(
                (item) => item?.deviceId === deviceId
            );
            if (!deviceList?.length || !currentDevice) {
                return res.status(400).json({
                    message: "Device not connected",
                    success: false,
                });
            }

            const attatchmentUpload = multer({
                storage: createStorage(
                    folderName ? folderName : "",
                    getStorageFolderPath(currentDevice as DeviceInfo)
                ),
            });
            attatchmentUpload.array(field_name, max_length)(
                req,
                res,
                async (err: any) => {
                    if (err) {
                        if (err?.code === "LIMIT_UNEXPECTED_FILE") {
                            return res.status(404).json({
                                message:
                                    "Maximum " +
                                    max_length +
                                    " Image You Can Upload.",
                            });
                        }
                    } else {
                        if (
                            (!req.files || req.files?.length === 0) &&
                            imageRequired
                        ) {
                            return res.status(404).json({
                                message: "Please upload some image",
                            });
                        } else {
                            //reduce image size

                            const files: any = req.files;
                            const savedMap: Record<string, number> = {};
                            for (const file of files) {
                                const updatedAt = await getUploadedFileInfo(
                                    file.path
                                ); // get the modified time
                                savedMap[file.filename] =
                                    updatedAt?.updatedAt as any;
                            }
                            res.status(201).json({
                                success: true,
                                dataMap: savedMap,
                            });
                        }
                    }
                }
            );
        } catch (err) {
            console.log("Err", err);
            res.status(404).json({
                message: "Server Error Found",
            });
        }
    };
};
