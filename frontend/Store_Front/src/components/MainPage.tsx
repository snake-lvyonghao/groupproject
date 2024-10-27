import { Box, Grid, GridItem } from "@chakra-ui/react";
import { useState } from "react";
import Cart from "./Cart";
import Navbar from "./Navbar";
import OrderHistory from "./OrderHistory";
import ProductList from "./ProductList";

export interface product {
  id: number;
  name: string;
  price: number;
}

export interface cartProduct {
  cart_id: number;
  id: number;
  name: string;
  price: number;
  quantity: number;
}

const MainPage = () => {
  //这个usestate用来控制主区域的显示页面.
  const [main, SetMain] = useState("Menu");
  //这个usestate用来控制购物车中的商品.
  const [cartProducts, setCartProducts] = useState<cartProduct[]>([]);

  const [customerId,setCustomerId]=useState(-1);

  //用于标识购物车中的记录。
  const [cartId, setCartId] = useState(0);

  let mainAreaComponent;
  if (main == "Menu") {
    mainAreaComponent = (
      <ProductList
      customerId={customerId}
      setCustomerId={setCustomerId}
        cartCount={cartId}
        SetCartCount={setCartId}
        cartProducts={cartProducts}
        Add={setCartProducts}
      />
    );
  } else if (main == "OrderHistory") {
    mainAreaComponent = <OrderHistory customerId={customerId}/>;
  } else {
    mainAreaComponent = (
      <Cart  customerId={customerId} cartProducts={cartProducts} Remove={setCartProducts} />
    );
  }

  return (
    <Box height="100vh" width="100vw">
      <h1>{customerId}</h1>
      <Grid
        height="100vh"
        templateAreas={`"header header" "nav main"`}
        gridTemplateRows="80px 1fr"
        gridTemplateColumns="300px 1fr"
      >
        <GridItem area="header" bg="tomato" p={2} color="white">
          <h1>STORE</h1>
        </GridItem>
        <GridItem area="nav" p={2} color="white">
          <Navbar onClick={SetMain} />
        </GridItem>
        <GridItem area="main" p={2} color="white">
          {mainAreaComponent}
        </GridItem>
      </Grid>
    </Box>
  );
};

export default MainPage;
