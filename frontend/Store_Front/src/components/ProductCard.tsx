import { Box, Button, Card, Input } from "@chakra-ui/react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { cartProduct, product } from "./MainPage";
import { toaster } from "./ui/toaster";

const schema = z.object({
  quantity: z.number().min(1, { message: "minimum number is 1" }),
});

type FormData = z.infer<typeof schema>;

interface props {
  product: product;
  cartCount: number;
  SetCartCount: (count: number) => void;
  cartProducts: cartProduct[];
  Add: (choice: cartProduct[]) => void;
}

const ProductCard = ({
  product,
  cartCount,
  SetCartCount,
  cartProducts,
  Add,
}: props) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = (data: FormData, product: product) => {
    const choice = {
      cart_id: cartCount,
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: data.quantity,
    };
    const newCartProducts = [...cartProducts, choice];
    Add(newCartProducts);
    SetCartCount(cartCount + 1);

    console.log(cartProducts);
    toaster.create({
      title: "Product added to cart.",
      type: "success",
    });
  };

  return (
    <form
      onSubmit={handleSubmit((data) => onSubmit(data, product))}
      key={product.id}
    >
      <Card.Root width="380px" variant="elevated" key={product.id}>
        <Card.Body gap="2">
          <Card.Title mb="2">{product.name}</Card.Title>
          <Card.Description>
            <Box minHeight="30px" maxHeight="30px" color="balck" fontSize="lg">
              ${product.price}
            </Box>
          </Card.Description>
        </Card.Body>
        <Card.Footer justifyContent="flex-end">
          {/* <StepperInput
            min={1}
            defaultValue="1"
            {...register("quantity", { valueAsNumber: true })}
            id="quantity"
          /> */}
          <Input
            {...register("quantity", { valueAsNumber: true })}
            defaultValue="1"
            min={1}
            width={10}
          ></Input>
          <Button variant="surface" type="submit">
            Add to cart
          </Button>
          <label>{errors.quantity?.message}</label>
        </Card.Footer>
      </Card.Root>
    </form>
  );
};

export default ProductCard;
