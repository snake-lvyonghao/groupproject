import { Stack } from "@chakra-ui/react";
import useGet from "../hooks/useGet";
import { cartProduct, product } from "./MainPage";
import ProductCard from "./ProductCard";
import { ProductsEndPoint, UserEndPoint } from "./services/EndPoints";
import { Toaster } from "./ui/toaster";
import { useEffect, useState } from "react";
import GETSenderNumber from "./RESTFul/GETSenderNumber";
import { useParams } from "react-router-dom";

const ENDPOINT = ProductsEndPoint;

//ProductList的这四个参数分别用来控制1.购物车商品id。2.设置购物车商品id。3.购物车商品。4.添加购物车商品
interface props {
  customerId:number;
  setCustomerId:(id:number)=>void;
  cartCount: number;
  SetCartCount: (count: number) => void;
  cartProducts: cartProduct[];
  Add: (choice: cartProduct[]) => void;
}


const ProductList = ({customerId, setCustomerId,cartCount, SetCartCount, cartProducts, Add }: props) => {
  const [status,setStatus]=useState(0);
  const [error,setError]=useState("");
  const {username}=useParams();
  const GetIdEndPoint=UserEndPoint+"/"+username;

  //调用hook获取商品列表,返回的data是product的数组
  const { data, err, isLoading } = useGet<product[]>(ENDPOINT);

  useEffect(()=>{

    GETSenderNumber(GetIdEndPoint,setStatus,setCustomerId,setError)

  },[GetIdEndPoint,setCustomerId])


  //这里是测试数据
  const product = {
    id: 1,
    name: "shit",
    price: 1000,
  };
  const testData = [product, product, product, product];

  return (
    <Stack gap="4" direction="row" wrap="wrap">
      <Toaster />
      {data?.map((Product) => (
        //表单上传时同时上传表格数据和当前的商品信息。
        <ProductCard
          product={Product}
          cartCount={cartCount}
          SetCartCount={SetCartCount}
          cartProducts={cartProducts}
          Add={Add}
          key={Product.id}
        />
      ))}
    </Stack>
  );
};

export default ProductList;
