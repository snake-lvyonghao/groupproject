import { Container, Stack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import useGet from "../hooks/useGet";
import OrderCard from "./OrderCard";
import { OrderEndPoint } from "./services/EndPoints";
import { Toaster } from "./ui/toaster";

export interface order {
  id: number;
  Product_name: string;
  totalQuantity: number;
  totalPrice: number;
}
//ProductList的这四个参数分别用来控制1.购物车商品id。2.设置购物车商品id。3.购物车商品。4.添加购物车商品

const OrderHistory = () => {
  //Order的endpoint应该是/order/{email}
  const { EmailAddress } = useParams();
  const ENDPOINT = OrderEndPoint + "/" + EmailAddress;

  const [data, setData] = useState<order[] | null>(null);
  const [statusCode, setStatusCode] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      const { data: fetchedData, error: fetchError } = await useGet<order[]>(
        ENDPOINT
      );
      if (fetchedData) setData(fetchedData);
      if (fetchError) setError(fetchError);
    };

    fetchData();
  }, [statusCode]);

  //调用hook获取商品列表,返回的data是product的数组

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
      {testData?.map((order) => (
        //表单上传时同时上传表格数据和当前的商品信息。

        <OrderCard
          order={order}
          setStatusCode={setStatusCode}
          setMessage={setMessage}
          setError={setError}
        />
      ))}
      {testData.length == 0 ? (
        <Container color="gray.300" width="100%">
          Nothing here.
        </Container>
      ) : null}
    </Stack>
  );
};

export default OrderHistory;
