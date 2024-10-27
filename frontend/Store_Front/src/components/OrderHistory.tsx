import { Container, Stack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
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
//ProductList的这四个参数分别用来控制1.购物车商品id。2.设置购物车商品id。3.购物车商品。4.添加购物车商品

const OrderHistory = ({customerId}:props) => {
  //Order的endpoint应该是/order/{email}
  const ENDPOINT = OrderEndPoint + "/" + customerId;

  const [statusCode, setStatusCode] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const[refresh,setRefesh]=useState(true);

    const {data,err,isLoading}=useGet<order[]>(ENDPOINT,undefined,[refresh])
  //调用hook获取商品列表,返回的data是product的数组

  //useffect用来监控status值和刷新页面
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
  //这里是测试数据
  const order = {
    id: 1,
    Product_name: "shit",
    totalQuantity: 1000,
    totalPrice: 10000,
  };
  const testData = [order, order, order, order];

  return (
    <Stack gap="4" direction="row" wrap="wrap">
      <Toaster />
      {data?.map((order) => (
        //表单上传时同时上传表格数据和当前的商品信息。

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
