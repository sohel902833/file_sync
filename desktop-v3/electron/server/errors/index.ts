export const createError = (
    error: string,
    opts?: {
        errorMessages: Record<string, any>;
    }
) => {
    const { errorMessages = {} } = opts || {};
    const newError = new Error(error);
    //@ts-ignore
    newError.errorMessages = errorMessages;
    throw newError;
};
