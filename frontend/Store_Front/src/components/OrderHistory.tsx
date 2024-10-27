import { Container, Stack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import useGet from "../hooks/useGet";
import OrderCard from "./OrderCard";
import { OrderEndPoint } from "./services/EndPoints";
import { toaster, Toaster } from "./ui/toaster";

interface props{
  customerId:number;
}
interface product{
  id:number;
  name:string;
  price:number;
}

export interface order {
  id: number;
  product:product;
  totalQuantity: number;
  orderDate:Date;
}


const OrderHistory = ({customerId}:props) => {

  const ENDPOINT = OrderEndPoint + "/" + customerId;

  const [statusCode, setStatusCode] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const[refresh,setRefesh]=useState(true);

    const {data,err,isLoading}=useGet<order[]>(ENDPOINT,undefined,[refresh])
  //use hook to get order history and data is product array

  //useffect to trace status code and refresh page
  useEffect(()=>{
    if(statusCode==200){
      toaster.create({
        "title":"Refound succeed!",
        "type":"success",
      });
    setRefesh(!refresh);}
      else if(statusCode==400||statusCode==500){
        toaster.create({
          "title":"Something Wrong.",
          "type":"error",
        })

      }
    },[statusCode])


  return (
    <Stack gap="4" direction="row" wrap="wrap">
      <Toaster />
      {data?.map((order) => (

        <OrderCard
        key={order.id}
        customerId={customerId}
          order={order}
          setStatusCode={setStatusCode}
          setMessage={setMessage}
          setError={setError}
        />
      ))}
      {data?.length == 0 ? (
        <Container color="gray.300" width="100%">
          Nothing here.
        </Container>
      ) : null}
    </Stack>
  );
};

export default OrderHistory;
