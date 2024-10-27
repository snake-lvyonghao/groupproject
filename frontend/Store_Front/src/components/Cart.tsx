import { Container, Stack } from "@chakra-ui/react";
import CartCard from "./CartCard";
import { cartProduct } from "./MainPage";
import { Toaster } from "./ui/toaster";

interface props {
  customerId:number;
  cartProducts: cartProduct[];
  Remove: (newCartProducts: cartProduct[]) => void;
}

const Cart = ({customerId,cartProducts, Remove }: props) => {
  return (
    <Stack gap="4" direction="row" wrap="wrap">
      <Toaster />
      {cartProducts?.map((cartProduct: cartProduct) => (
        <CartCard
        key={cartProduct.cart_id}
        customerId={customerId}
          cartProduct={cartProduct}
          cartProducts={cartProducts}
          Remove={Remove}
        />
      ))}
      {cartProducts.length === 0 ? (
        <Container color="gray.300" width="100%">
          Nothing here.
        </Container>
      ) : null}
    </Stack>
  );
};

export default Cart;
