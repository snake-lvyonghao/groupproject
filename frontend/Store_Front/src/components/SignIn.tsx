import { Box, Button, Card, Center, Input, Stack } from "@chakra-ui/react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { Field } from "../components/ui/field";
import { PasswordInput } from "./ui/password-input";

const schema = z.object({
  EmailAddress: z.string().email({ message: "Please enter a valid email." }),
  Password: z.string().min(1, { message: "please input password." }),
});

type FormData = z.infer<typeof schema>;

const SignIn = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const [status, setStatus] = useState(0);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  //navigate
  const navigate = useNavigate();

  //用useEffect来检测登录状态登录成功时跳转。
  useEffect(() => {
    if (status != 0) console.log(status);
    if (message) console.log(message);
    if (error) console.log(error);
  }, [status, message, error]);

  const onSubmit = (data: FormData) => {
    console.log(data);
    navigate(`/mainpage/${data.EmailAddress}`);
  };

  return (
    <Box height="100vh" width="100vw">
      <Center>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Card.Root maxW="sm">
            <Card.Header>
              <Card.Title>STORE</Card.Title>
            </Card.Header>
            <Card.Body>
              <Stack gap="4" w="full">
                <Field label="Email" invalid={!!errors.EmailAddress}>
                  <Input
                    {...register("EmailAddress", {
                      required: "email is required",
                    })}
                    id="email"
                    type="email"
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.EmailAddress?.message}
                  </Box>
                </Field>

                <Field label="Password" invalid={!!errors.Password}>
                  <PasswordInput
                    {...register("Password", {
                      required: "password is required",
                    })}
                  />
                  <Box minHeight="20px" color="red.500" fontSize="sm">
                    {errors.Password?.message}
                  </Box>
                </Field>
              </Stack>
            </Card.Body>
            <Card.Footer justifyContent="flex-end">
              <Button type="submit" color="black">
                Sign In
              </Button>
            </Card.Footer>
          </Card.Root>
        </form>
      </Center>
    </Box>
  );
};

export default SignIn;
