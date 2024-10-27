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
  //control the content in mainarea
  const [main, SetMain] = useState("Menu");
  //control products in cart
  const [cartProducts, setCartProducts] = useState<cartProduct[]>([]);

  const [customerId,setCustomerId]=useState(-1);

  //mark product in cart
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
