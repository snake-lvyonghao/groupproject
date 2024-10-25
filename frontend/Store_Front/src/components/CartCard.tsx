import { Box, Button, Card, HStack, useDisclosure } from "@chakra-ui/react";
import React from "react";
import { cartProduct } from "./MainPage";
import {
  DialogActionTrigger,
  DialogBody,
  DialogCloseTrigger,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogRoot,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import { toaster } from "./ui/toaster";

interface props {
  cartProduct: cartProduct;
  cartProducts: cartProduct[];
  Remove: (choice: cartProduct[]) => void;
}

const CartCard = ({ cartProduct, cartProducts, Remove }: props) => {
  //弹窗控制
  const { isOpen, onOpen, onClose } = useDisclosure();
  const cancelRef = React.useRef<HTMLButtonElement>(null);

  const onClick = () => {
    //1.发送POST请求。
    const order = {
      id: cartProduct.id,
      name: cartProduct.name,
      price: cartProduct.price,
      quantity: cartProduct.quantity,
    };
    //这里还差发送POST的逻辑

    //2.从购物车中删除该商品.
    console.log(cartProduct.cart_id);
    const newCartProducts = cartProducts.filter(
      (product) => product.cart_id != cartProduct.cart_id
    );
    Remove(newCartProducts);

    toaster.create({
      title: "Check out Successfully.",
      type: "success",
    });
  };

  return (
    <Card.Root width="90% " variant="elevated" key={cartProduct.id}>
      <Card.Body gap="2">
        <Card.Title mb="2">{cartProduct.name}</Card.Title>
        <Card.Description>
          <HStack spaceX={20} justifyContent="space-between">
            <Box
              minHeight="30px"
              maxHeight="30px"
              maxWidth="200px"
              color="balck"
              fontSize="lg"
            >
              Single Price: ${cartProduct.price}
            </Box>
            <Box
              minHeight="30px"
              maxHeight="30px"
              maxWidth="200px"
              color="balck"
              fontSize="lg"
            >
              Quantity: {cartProduct.quantity}
            </Box>
            <Box
              minHeight="30px"
              maxHeight="30px"
              maxWidth="200px"
              color="balck"
              fontSize="lg"
            >
              Total Price: ${cartProduct.price * cartProduct.quantity}
            </Box>
            {/* <Button variant="surface" onClick={onClick}>
              Check Out
            </Button> */}
            <DialogRoot>
              <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                  Check Out
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Order Detail</DialogTitle>
                </DialogHeader>
                <DialogBody>
                  <p>
                    The total price is $
                    {cartProduct.price * cartProduct.quantity}.
                  </p>
                </DialogBody>
                <DialogFooter>
                  <DialogActionTrigger asChild>
                    <Button variant="plain" onClick={onClick} bgColor="red">
                      Pay
                    </Button>
                  </DialogActionTrigger>
                </DialogFooter>
                <DialogCloseTrigger />
              </DialogContent>
            </DialogRoot>
          </HStack>
        </Card.Description>
      </Card.Body>
      <Card.Footer justifyContent="flex-end"></Card.Footer>
    </Card.Root>
  );
};

export default CartCard;
