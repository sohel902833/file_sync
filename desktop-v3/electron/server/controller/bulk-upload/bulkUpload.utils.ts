import { existsSync, mkdirSync } from "fs";
import multer from "multer";
import path from "path";

export const createStorage = (folderName: string, deviceFilePath: string) => {
    const directory = path.join(deviceFilePath, folderName);
    const folderExists = existsSync(directory);
    try {
        if (!folderExists) {
            mkdirSync(directory);
        }
        return multer.diskStorage({
            destination: function (req, file, cb) {
                cb(null, directory);
            },
            filename: function (req, file, cb) {
                const fileName = file.originalname;
                cb(null, fileName);
            },
        });
    } catch (err) {
        console.log("File Creating error", err);
    }
};
