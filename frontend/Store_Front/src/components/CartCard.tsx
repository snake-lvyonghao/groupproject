import { Box, Button, Card, HStack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { cartProduct } from "./MainPage";
import PostSender from "./RESTFul/PostSender";
import { OrderEndPoint } from "./services/EndPoints";
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
import { Toaster, toaster } from "./ui/toaster";

interface props {
  customerId:number;
  cartProduct: cartProduct;
  cartProducts: cartProduct[];
  Remove: (choice: cartProduct[]) => void;
}

const CartCard = ({customerId,cartProduct, cartProducts, Remove }: props) => {
  const ENDPOINT = OrderEndPoint;

  const [status, setStatus] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  //用useEffect来检测订单提交的返回值;
  useEffect(() => {
    if (status === 200) {
      console.log("order created");
      
      toaster.create({
        title: "Check out successfully.",
        type: "success",
      });

      //从购物车中移除商品
      const newCartProducts = cartProducts.filter(
        (product) => product.cart_id != cartProduct.cart_id
      );
       Remove(newCartProducts);
    }
    if (message) console.log(message);
    if (error) console.log(error);
  }, [status, message, error]);

  const onClick = () => {
    //1.发送POST请求。
    const order = {
      goodsId: cartProduct.id,
      customerId:customerId,
      quantity: cartProduct.quantity,
    };
    //这里还差发送POST的逻辑
    PostSender(ENDPOINT, order, setStatus, setMessage, setError);

    //2.从购物车中删除该商品.
  };

  return (
    <Card.Root width="90% " variant="elevated" key={cartProduct.id}>
      <Toaster />
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
