import { Stack } from "@chakra-ui/react";
import CartCard from "./CartCard";
import { cartProduct } from "./MainPage";

interface props {
  cartProducts: cartProduct[];
  Remove: (newCartProducts: cartProduct[]) => void;
}

const Cart = ({ cartProducts, Remove }: props) => {
  return (
    <Stack gap="4" direction="row" wrap="wrap">
      {cartProducts?.map((cartProduct: cartProduct) => (
        <CartCard
          cartProduct={cartProduct}
          cartProducts={cartProducts}
          Remove={Remove}
        />
      ))}
    </Stack>
  );
};

export default Cart;
