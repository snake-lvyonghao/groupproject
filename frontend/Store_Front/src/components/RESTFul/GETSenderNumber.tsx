import { AxiosRequestConfig, CanceledError } from "axios";
import apiClient from "../services/api-client";

interface ApiResponse {
  data:number;
}

const GETSenderNumber = (
  endpoint: string,
  setStatus: (code: number) => void,
  setData: (data:number) => void,
  setError: (error: string) => void,
  requestConfig?: AxiosRequestConfig
) => {
  const controller = new AbortController();

  console.log("GetSender is working");
  apiClient
    .get<number>(endpoint, {
      signal: controller.signal,
      ...requestConfig,
    })
    .then((res) => {
      //   console.log(res.status + " " + res.data.message);
      setStatus(res.status);
      setData(res.data);
     
    })
    .catch((err) => {
      if (err instanceof CanceledError) return;
      if (err.response.data) {
        setStatus(err.status);
        setError(err.response.data.message);
      }
    });
};

export default GETSenderNumber;
