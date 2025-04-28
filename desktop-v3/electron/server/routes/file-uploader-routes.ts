import { Router } from "express";
import { uploadFiles } from "../controller/bulk-upload/bulkUpload.controller";
const router = Router();

router.post(
    "/upload-files/:deviceId/:folderName",
    uploadFiles(200, "files", true)
);

export default router;
