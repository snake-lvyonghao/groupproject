import { Stack } from "@chakra-ui/react";
import useGet from "../hooks/useGet";
import { cartProduct, product } from "./MainPage";
import ProductCard from "./ProductCard";
import { ProductsEndPoint } from "./services/EndPoints";
import { Toaster } from "./ui/toaster";

const ENDPOINT = ProductsEndPoint;

//ProductList的这四个参数分别用来控制1.购物车商品id。2.设置购物车商品id。3.购物车商品。4.添加购物车商品
interface props {
  cartCount: number;
  SetCartCount: (count: number) => void;
  cartProducts: cartProduct[];
  Add: (choice: cartProduct[]) => void;
}

const ProductList = ({ cartCount, SetCartCount, cartProducts, Add }: props) => {
  //调用hook获取商品列表,返回的data是product的数组
  const { data, error, isLoading } = useGet<product[]>(ENDPOINT);

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
      {testData?.map((Product) => (
        //表单上传时同时上传表格数据和当前的商品信息。
        <ProductCard
          product={Product}
          cartCount={cartCount}
          SetCartCount={SetCartCount}
          cartProducts={cartProducts}
          Add={Add}
        />
      ))}
    </Stack>
  );
};

export default ProductList;
