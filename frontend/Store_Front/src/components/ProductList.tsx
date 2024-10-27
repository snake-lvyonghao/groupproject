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

//these parameters are 1. customerID.2.used to get and refresg customerId 3.id of products in the cart 4.set the id 5. products in cart.6. modify cart
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

  const { data, err, isLoading } = useGet<product[]>(ENDPOINT);

  useEffect(()=>{

    GETSenderNumber(GetIdEndPoint,setStatus,setCustomerId,setError)

  },[GetIdEndPoint,setCustomerId])



  return (
    <Stack gap="4" direction="row" wrap="wrap">
      <Toaster />
      {data?.map((Product) => (
        //upload formdata and product inform at the same time 
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
